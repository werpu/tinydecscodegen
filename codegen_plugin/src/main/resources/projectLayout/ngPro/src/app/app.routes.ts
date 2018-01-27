/**
 * global route declarations
 *
 * Note, if you dont have local routes, simply
 * import localRoutesProvider(<your routes>)
 */
import {FirstPageComponent} from "./firstpage/first-page-module/pages/first-page/first-page.component";
import {UIRouterModule} from "@uirouter/angular";
import {MainPageComponent} from "./firstpage/first-page-module/pages/main-page/main-page.component";

/*
 * add your global route definitions here
 */
const defaultPageState: any = { name: 'default', url: '',  component: MainPageComponent };
const mainPageState: any = { name: 'mainpage', url: '/mainpage',  component: MainPageComponent };
const aboutState: any = { name: 'firstpage', url: '/firstpage',  component: FirstPageComponent };


export let moduleWithProviders = UIRouterModule.forRoot({ states: [defaultPageState, mainPageState, aboutState ], useHash: true });

export function localRoutesProvider(routes ?: any): any {
  if(!routes) {
    return UIRouterModule.forChild({});
  }
  return UIRouterModule.forChild(routes);
}
