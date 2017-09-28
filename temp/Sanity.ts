export class Sanity {

    constructor(public prop1: number = 0, prop2: string = "hello world",
                prop3: Date = new Date(), private _param4 = "hello2") {
    }

    get param4(): string {
        return this._param4+"hello world"
    }
}