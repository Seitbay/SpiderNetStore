import React, { useState, createContext, useContext } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';

const SidebarContext = createContext({ collapsed: false, setCollapsed: () => {} });
export const useSidebar = () => useContext(SidebarContext);

export default function AppLayout() {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <SidebarContext.Provider value={{ collapsed, setCollapsed }}>
      <div className="flex min-h-screen">
        <Sidebar />
        <main
          className={`flex-1 min-h-screen transition-all duration-300 ${collapsed ? 'ml-16' : 'ml-60'}`}
        >
          <div className="max-w-7xl mx-auto px-6 py-8">
            <Outlet />
          </div>
        </main>
      </div>
    </SidebarContext.Provider>
  );
}
