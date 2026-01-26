<template>
  <van-nav-bar title="设备报修系统" />
  <van-tabbar route>
    <van-tabbar-item to="/" icon="home-o">首页</van-tabbar-item>
    <van-tabbar-item to="/report" icon="edit">报修</van-tabbar-item>
    <van-tabbar-item to="/tickets" icon="records">工单</van-tabbar-item>
    <van-tabbar-item to="/technician" icon="manager">维修端</van-tabbar-item>
    <van-tabbar-item to="/admin" icon="setting-o">管理端</van-tabbar-item>
  </van-tabbar>
  <router-view />
</template>
