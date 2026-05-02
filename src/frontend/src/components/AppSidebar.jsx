import React from 'react'
import { useSelector, useDispatch } from 'react-redux'

import {
  CCloseButton,
  CSidebar,
  CSidebarBrand,
  CSidebarFooter,
  CSidebarHeader,
  CSidebarToggler,
} from '@coreui/react'

import { AppSidebarNav } from './AppSidebarNav'
import logo from '../logo/brand/logo.jpg'
import navigation from '../_nav'

const AppSidebar = () => {

  const dispatch = useDispatch()
  const unfoldable = useSelector((state) => state.sidebarUnfoldable)
  const sidebarShow = useSelector((state) => state.sidebarShow)

  return (

    <CSidebar
      className="border-end"
      colorScheme="dark"
      position="fixed"
      unfoldable={unfoldable}
      visible={sidebarShow}
      onVisibleChange={(visible)=>{
        dispatch({ type:'set', sidebarShow:visible })
      }}
    >

      {/* HEADER */}
      <CSidebarHeader className="border-bottom">

        <CSidebarBrand
          to="/"
          style={{
            display:"flex",
            alignItems:"center",
            gap:"10px",
            paddingLeft:"10px"
          }}
        >

          {/* LOGO */}
          <img
            src={logo}
            alt="EMMS"
            style={{ height:"35px" }}
          />

          {/* TEXT */}
          <span
            style={{
              color:"white",
              fontWeight:"700",
              fontSize:"20px",
              letterSpacing:"2px"
            }}
          >
            EMMS
          </span>

        </CSidebarBrand>

        <CCloseButton
          className="d-lg-none"
          dark
          onClick={()=>dispatch({ type:'set', sidebarShow:false })}
        />

      </CSidebarHeader>


      {/* MENU */}
      <AppSidebarNav items={navigation} />


      {/* FOOTER */}
      <CSidebarFooter className="border-top d-none d-lg-flex">
        <CSidebarToggler
          onClick={()=>dispatch({ type:'set', sidebarUnfoldable:!unfoldable })}
        />
      </CSidebarFooter>

    </CSidebar>

  )
}

export default React.memo(AppSidebar)