import {Component, Inject, OnInit} from '@angular/core';
import {StateService} from '@uirouter/angular';

@Component({
  selector: 'app-main-page',
  templateUrl: './main-page.component.html',
  styleUrls: ['./main-page.component.css']
})
export class MainPageComponent implements OnInit {

  constructor(@Inject(StateService) private stateService: StateService) { }

  ngOnInit() {
  }

  navIt() {
    this.stateService.go('firstpage');
  }

}
