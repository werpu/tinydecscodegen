import {Injectable} from "@angular/core";
import {HTTP_INTERCEPTORS} from "@angular/common/http";
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs/Observable";

@Injectable()
export class RestHttpInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    //add your own request interceptor code here
    /*let newReq = req.clone({
        url: "/booga"+req.url
    });*/

    return next.handle(req);//you can also intercept the response here
  }
}

export let HttpInterceptorService =  {
  provide: HTTP_INTERCEPTORS,
  useClass: RestHttpInterceptor,
  multi: true
};
