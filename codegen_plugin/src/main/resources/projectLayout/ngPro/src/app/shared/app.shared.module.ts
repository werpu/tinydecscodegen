import {NgModule} from '@angular/core';
import {BrowserModule} from "@angular/platform-browser";
import {HttpClientModule} from "@angular/common/http";
import {CommonModule} from "@angular/common";

/**
 * Module SharedModule
 * @author ${AUTHOR}
 *
 * This is a passthrough module which bundles all
 * imported component sets shared over all modules
 *
 */

@NgModule({
    declarations: [],
    imports: [BrowserModule, CommonModule, HttpClientModule],
    exports: [BrowserModule, CommonModule, HttpClientModule]
})
export class SharedModule { }
