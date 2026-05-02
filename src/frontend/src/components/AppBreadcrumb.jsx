import React from 'react'
import { useLocation } from 'react-router-dom'
import { CBreadcrumb, CBreadcrumbItem } from '@coreui/react'
import routes from '../routes'

const AppBreadcrumb = () => {
  const currentLocation = useLocation().pathname

  const normalizeRoutePath = (path) => {
    return path?.replace(/\/\*$/, '') || ''
  }

  const getRouteName = (pathname, routes) => {
    const currentRoute = routes.find((route) => normalizeRoutePath(route.path) === pathname)
    return currentRoute ? currentRoute.name : null
  }

  const getBreadcrumbs = (pathname) => {
    const breadcrumbs = []
    const pathSegments = pathname.split('/').filter(Boolean)

    let currentPath = ''

    pathSegments.forEach((segment, index) => {
      currentPath += `/${segment}`
      const routeName = getRouteName(currentPath, routes)

      if (
        routeName &&
        currentPath !== '/dashboard' &&
        currentPath !== '/logo'
      ) {
        breadcrumbs.push({
          pathname: currentPath,
          name: routeName,
          active: index === pathSegments.length - 1,
        })
      }
    })

    return breadcrumbs
  }

  const breadcrumbs = getBreadcrumbs(currentLocation)

  return (
    <CBreadcrumb
      className="my-0 py-1"
      style={{
        fontSize: '13px',
        marginBottom: 0,
      }}
    >
      <CBreadcrumbItem href="#/dashboard">Trang chủ</CBreadcrumbItem>

      {breadcrumbs.map((breadcrumb, index) => (
        <CBreadcrumbItem
          key={index}
          {...(breadcrumb.active ? { active: true } : { href: `#${breadcrumb.pathname}` })}
        >
          {breadcrumb.name}
        </CBreadcrumbItem>
      ))}
    </CBreadcrumb>
  )
}

export default React.memo(AppBreadcrumb)