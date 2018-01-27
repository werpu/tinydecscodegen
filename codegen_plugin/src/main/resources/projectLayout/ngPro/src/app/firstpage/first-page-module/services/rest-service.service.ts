import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {HelloDto} from '../dtos/HelloDto';

@Injectable()
export class RestServiceService {

  constructor(@Inject(HttpClient) private httpClient: HttpClient) {

  }

  sayHello(): Observable<HelloDto> {
    return <Observable<HelloDto>> this.httpClient.get('/rest/hello/sayHello');
  }


}
