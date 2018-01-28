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
