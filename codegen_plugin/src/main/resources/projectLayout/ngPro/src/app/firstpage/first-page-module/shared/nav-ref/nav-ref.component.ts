import {Component, Inject, Input, OnInit} from '@angular/core';
import {StateService} from '@uirouter/angular';

@Component({
  selector: 'app-nav-ref',
  template: `
    <a (click)="nav()" href="javascript:;"><ng-content></ng-content></a>
  `,
  styles: []
})
export class NavRefComponent implements OnInit {

  @Input() state: string;



  constructor(private stateService: StateService) {

  }

  ngOnInit() {
  }

  nav() {
    this.stateService.go(this.state);
  }

}
