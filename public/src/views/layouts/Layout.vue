<template>
    <div class="app-wrapper">
        <side-bar class="sidebar-container" />
        <div class="main-container">
            <nav-bar/>
            <app-main/>
        </div>
    </div>
</template>


<script>
import {AppMain, NavBar, SideBar} from './sublayers'

export default {
    name : "Layout",
    components: {
        AppMain,
        NavBar,
        SideBar
    }
}
</script>


