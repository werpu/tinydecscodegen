import {Component, Inject, OnInit} from '@angular/core';
import {RestServiceService} from '../../services/rest-service.service';
import {HelloDto} from '../../dtos/HelloDto';

@Component({
  selector: 'app-first-page',
  templateUrl: './first-page.component.html',
  styleUrls: ['./first-page.component.css']
})
export class FirstPageComponent implements OnInit {

  constructor(@Inject(RestServiceService) private restService: RestServiceService) { }

  ngOnInit() {

  }

  callSayHello() {
    this.restService.sayHello().subscribe((data: HelloDto) => {
        alert(JSON.stringify(data));
    });
  }
}

