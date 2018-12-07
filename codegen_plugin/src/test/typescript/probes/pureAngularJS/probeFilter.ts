import TranslationService from "../service/TranslationService";

export class EnumTranslateFilter {

    static $inject = ['TranslationService'];

    constructor(translationService: TranslationService) {
        return function (enumValue: string, enumType: string) {
            return translationService.translateEnum(enumType, enumValue)
        }
    }
}