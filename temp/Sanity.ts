export class Sanity {

    constructor(public prop1: number = 0, prop2: string = "hello world",
                prop3: Date = new Date(), private _param4 = "hello2") {
    }

    set param4(param4: string) {
        this._param4 = param4;
    }

    get param4(): string {
        return this._param4+"hello world"
    }
}