import {APP_INITIALIZER} from '@angular/core';

/**
 * This is a startup deferring application init function
 * it can be used to preload data
 *
 * @returns {any}
 * @constructor
 */
export function OnAppInit(/*deps*/): any  {
  return function ():Promise<any> {
    return new Promise((resolve, reject) => {
      //add your deferring init code here
      resolve();
    });
  }
}

export let AppInitializerService = {
  provide: APP_INITIALIZER,
  useFactory: OnAppInit,
  multi: true,
  deps: [/*dependencies like services used by the init function must!!! be declared here*/]
};
