package ru.SeitbayBulat.SpiderNetStore.product.productServices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.SeitbayBulat.SpiderNetStore.product.dto.ArchiveBase64Dto;
import ru.SeitbayBulat.SpiderNetStore.product.dto.CreateProductRequest;
import ru.SeitbayBulat.SpiderNetStore.product.dto.StockAppendDto;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockPayloadType;
import ru.SeitbayBulat.SpiderNetStore.product.stock.StockService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductStockImportService {

    private static final int MAX_ARCHIVE_BYTES = 50 * 1024 * 1024;
    private static final int PREVIEW_MAX = 200;

    private final StockService stockService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void importFromStockAppend(Long productId, StockAppendDto dto) {
        if (dto == null) {
            return;
        }
        CreateProductRequest req = new CreateProductRequest();
        req.setTextLineBatches(dto.getTextLineBatches());
        req.setJsonFileContents(dto.getJsonFileContents());
        req.setArchivesBase64(dto.getArchivesBase64());
        importFromCreateRequest(productId, req);
    }

    @Transactional
    public void importFromCreateRequest(Long productId, CreateProductRequest req) {
        if (req.getTextLineBatches() != null) {
            for (List<String> batch : req.getTextLineBatches()) {
                if (batch == null) {
                    continue;
                }
                for (String line : batch) {
                    if (line == null || line.isBlank()) {
                        continue;
                    }
                    addTextLine(productId, line.trim());
                }
            }
        }
        if (req.getJsonFileContents() != null) {
            for (String fileContent : req.getJsonFileContents()) {
                if (fileContent == null || fileContent.isBlank()) {
                    continue;
                }
                parseJsonFileContent(productId, fileContent.trim());
            }
        }
        if (req.getArchivesBase64() != null) {
            for (ArchiveBase64Dto a : req.getArchivesBase64()) {
                if (a == null || a.getBase64() == null || a.getBase64().isBlank()) {
                    continue;
                }
                byte[] bytes = Base64.getDecoder().decode(a.getBase64().trim());
                validateArchiveSize(bytes.length);
                String meta = buildArchiveMetaJson(a.getFilename(), bytes.length);
                stockService.addStockItem(productId, StockPayloadType.ARCHIVE_FILE, meta, bytes, safeName(a.getFilename()));
            }
        }
    }

    @Transactional
    public void importTextFiles(Long productId, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                br.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(line -> addTextLine(productId, line));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось прочитать текстовый файл: " + e.getMessage());
            }
        }
    }

    @Transactional
    public void importJsonFiles(Long productId, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                parseJsonFileContent(productId, content);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось прочитать JSON-файл: " + e.getMessage());
            }
        }
    }

    @Transactional
    public void importArchiveFiles(Long productId, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            try {
                byte[] bytes = file.getBytes();
                validateArchiveSize(bytes.length);
                String fn = file.getOriginalFilename() != null ? file.getOriginalFilename() : "archive.bin";
                String meta = buildArchiveMetaJson(fn, bytes.length);
                stockService.addStockItem(productId, StockPayloadType.ARCHIVE_FILE, meta, bytes, fn);
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не удалось прочитать архив: " + e.getMessage());
            }
        }
    }

    private void parseJsonFileContent(Long productId, String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            if (root.isArray()) {
                for (JsonNode el : root) {
                    stockService.addStockItem(productId, StockPayloadType.JSON_OBJECT, el.toString(), null, null);
                }
            } else if (root.isObject()) {
                stockService.addStockItem(productId, StockPayloadType.JSON_OBJECT, root.toString(), null, null);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON-файл должен содержать объект или массив объектов");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный JSON: " + e.getMessage());
        }
    }

    private void addTextLine(Long productId, String line) {
        try {
            ObjectNode n = objectMapper.createObjectNode();
            n.put("line", line);
            stockService.addStockItem(productId, StockPayloadType.TEXT_LINE, objectMapper.writeValueAsString(n), null, null);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка формирования строки склада");
        }
    }

    private String buildArchiveMetaJson(String filename, int size) {
        try {
            ObjectNode n = objectMapper.createObjectNode();
            n.put("fileName", filename != null ? filename : "archive");
            n.put("size", size);
            return objectMapper.writeValueAsString(n);
        } catch (Exception e) {
            return "{\"fileName\":\"archive\",\"size\":" + size + "}";
        }
    }

    private static void validateArchiveSize(int len) {
        if (len > MAX_ARCHIVE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Архив больше " + MAX_ARCHIVE_BYTES + " байт");
        }
    }

    private static String safeName(String filename) {
        return filename != null ? filename : "archive.bin";
    }

    public static String previewData(String data) {
        if (data == null) {
            return null;
        }
        return data.length() <= PREVIEW_MAX ? data : data.substring(0, PREVIEW_MAX) + "…";
    }
}
