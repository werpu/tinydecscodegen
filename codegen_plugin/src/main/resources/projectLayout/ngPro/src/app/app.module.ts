import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {FirstPageModuleModule} from './firstpage/first-page-module/first-page-module.module';
import {MainPageComponent} from './firstpage/first-page-module/pages/main-page/main-page.component';
import {FirstPageComponent} from './firstpage/first-page-module/pages/first-page/first-page.component';
import {UIRouterModule} from '@uirouter/angular';

const defaultPageState: any = { name: 'default', url: '',  component: MainPageComponent };
const mainPageState: any = { name: 'mainpage', url: '/mainpage',  component: MainPageComponent };
const aboutState: any = { name: 'firstpage', url: '/firstpage',  component: FirstPageComponent };



@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule, UIRouterModule.forRoot({ states: [defaultPageState, mainPageState, aboutState ], useHash: true }), FirstPageModuleModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
