/**
 * global route declarations
 *
 * Note, if you dont have local routes, simply
 * import localRoutesProvider(<your routes>)
 */
import {FirstPageComponent} from "./firstpage/first-page-module/pages/first-page/first-page.component";
import {Ng2StateDeclaration, UIRouterModule} from "@uirouter/angular";
import {MainPageComponent} from "./firstpage/first-page-module/pages/main-page/main-page.component";
import {ModuleWithProviders} from "@angular/core";
import {StatesModule} from "@uirouter/angular/lib/uiRouterNgModule";

/*
 * add your global route definitions here
 */
const defaultPageState: Ng2StateDeclaration = {name: 'default', url: '', component: MainPageComponent};
const mainPageState: Ng2StateDeclaration = {name: 'mainpage', url: '/mainpage', component: MainPageComponent};
const subRoute: Ng2StateDeclaration = {name: 'mainpage.sub', url: '/mainpage/sub', component: MainPageComponent};
const aboutState: Ng2StateDeclaration = {name: 'firstpage', url: '/firstpage', component: FirstPageComponent};

/**
 *
 * Main routes provider in this application
 */
export let rootRoutesProvider = UIRouterModule.forRoot(
    {
        states: [defaultPageState, mainPageState, aboutState], //the currently implemented main routes
        useHash: true,                //use the hashbang for old browser support
        deferInitialRender: true,     //defer the initial rendering until the init code is done
        otherwise: ""   //default state if there is no match
    }
);

/**
 * helper function to import all needed components into a child module
 * if you dont import this function uiSref will not work
 *
 * @param routes your own local module routes
 *
 * @returns {ModuleWithProviders}
 */
export function localRoutesProvider(routes ?: StatesModule): ModuleWithProviders {
    if(!routes) {
        return UIRouterModule.forChild({});
    }
    return UIRouterModule.forChild(routes);
}

