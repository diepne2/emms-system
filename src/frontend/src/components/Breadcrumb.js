import React from "react";
import { useLocation } from "react-router-dom";
import { CBreadcrumb, CBreadcrumbItem } from "@coreui/react";

import routes from "../routes";

const AppBreadcrumb = () => {

  // Lấy đường dẫn hiện tại
  const location = useLocation();
  const pathname = location.pathname;

  // Tìm tên route theo path
  const findRouteName = (path) => {
    const route = routes.find((item) => item.path === path);
    return route ? route.name : null;
  };

  // Tạo danh sách breadcrumb
  const createBreadcrumbs = () => {

    const pathParts = pathname.split("/");
    let currentPath = "";

    const breadcrumbList = [];

    pathParts.forEach((part, index) => {

      if (part === "") return;

      currentPath += "/" + part;

      const name = findRouteName(currentPath);

      if (name) {
        breadcrumbList.push({
          name: name,
          path: currentPath,
          active: index === pathParts.length - 1
        });
      }

    });

    return breadcrumbList;
  };

  const breadcrumbs = createBreadcrumbs();

  return (
    <CBreadcrumb className="my-0">

      {/* Trang Home */}
      <CBreadcrumbItem href="/">
        Home
      </CBreadcrumbItem>

      {breadcrumbs.map((item, index) => (
        <CBreadcrumbItem
          key={index}
          {...(item.active ? { active: true } : { href: item.path })}
        >
          {item.name}
        </CBreadcrumbItem>
      ))}

    </CBreadcrumb>
  );
};

export default React.memo(AppBreadcrumb);