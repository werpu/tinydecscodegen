/**
 * @def: i18nfile
 * note, the def section marks this file as i18n file
 * so that the parser can clearly start to parse here
 */
export const language = {
    test3: {
        test4: "booga",
        test: "testString_xx",
        testy: `testString_xx
            booga `,
        testx: () => {alert("bla");}
    },
    test: "testString",
    test2: {
        test4: "booga",
        test3: {
            test4: "booga2",
            test5: "booga3"
        }
    },
    /*special entries unrelated to the i18n file*/
    shared: {
        unknownFailure: 'Unknown failure.',
        validDate: (format: String) => `Date format ${format}`,
        fromDate: (format: String) => `Date from ${format}`,
        toDate: (format: String) => `Date to ${format}`,
        fromValid: (format: String) => `Valid from ${format}`,
        action: 'Action',
    },
}