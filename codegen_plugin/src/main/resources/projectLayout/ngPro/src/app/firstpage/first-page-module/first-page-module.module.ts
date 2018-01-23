import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FirstPageComponent } from './pages/first-page/first-page.component';
import { MainPageComponent } from './pages/main-page/main-page.component';
import { SecondPageComponent } from './pages/second-page/second-page.component';
import { NavRefComponent } from './shared/nav-ref/nav-ref.component';
import {HttpClientModule} from '@angular/common/http';
import {RestServiceService} from './services/rest-service.service';



@NgModule({
  declarations: [FirstPageComponent, MainPageComponent, SecondPageComponent, NavRefComponent],
  imports: [CommonModule, HttpClientModule]
})
export class FirstPageModuleModule { }
