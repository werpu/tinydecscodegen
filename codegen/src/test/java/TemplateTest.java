/*
Copyright 2017 Werner Punz

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import org.junit.Test;
import probes.TestProbeController;
import refactor.TinyRefactoringUtils;
import reflector.SpringJavaRestReflector;
import reflector.TypescriptRestGenerator;
import rest.RestService;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;



public class TemplateTest {

    static String probe = "/// <reference path=\"../../../dist/TinyDecorations.d.ts\" />\n" +
            "/// <reference path=\"../../../dist/Cache.d.ts\" />\n" +
            "/// <reference path=\"../../../dist/Routing.d.ts\" />\n" +
            "// Declare app level module which depends on views, and components\n" +
            "import {View2Module} from \"./view2/View2Module\";\n" +
            "import {View1Module} from \"./view1/View1Module\";\n" +
            "import {VersionModule} from \"./components/VersionModule\";\n" +
            "import {ILocationProvider} from \"angular\";\n" +
            "import ngResource = require(\"angular-resource\");\n" +
            "import {Config, Inject, keepExternals, NgModule, platformBrowserDynamic, Run} from \"TinyDecorations\";\n" +
            "\n" +
            "keepExternals(ngResource)\n" +
            "\n" +
            "\n" +
            "@Config()\n" +
            "export class AppConfig {\n" +
            "    constructor(@Inject(\"$locationProvider\") private $locationProvider: ILocationProvider,\n" +
            "                @Inject(\"$routeProvider\") private $routeProvider: any) {\n" +
            "        $locationProvider.hashPrefix('!');\n" +
            "        $routeProvider.otherwise({redirectTo: '/view1'});\n" +
            "        console.log(\"config called\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "@Run()\n" +
            "export class AppRun {\n" +
            "    constructor() {\n" +
            "        console.log(\"run called\");\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "@NgModule({\n" +
            "    name: \"myApp\",\n" +
            "    imports: [\"ngRoute\",\"ngResource\",\n" +
            "        View2Module,\n" +
            "        VersionModule, View1Module],\n" +
            "    declarations: [AppConfig, AppRun]\n" +
            "})\n" +
            "export class MyApp {\n" +
            "}\n" +
            "\n" +
            "\n" +
            "/*now lets bootstrap the application, unfortunately ng-app does not work due to the systemjs lazy binding*/\n" +
            "platformBrowserDynamic().bootstrapModule(MyApp);\n";
    @Test
    public void testBasicTemplating()  {
        List<RestService> services = SpringJavaRestReflector.reflectRestService(Arrays.asList(TestProbeController.class), true);
        TypescriptRestGenerator.generate(services, false);

        assertTrue(true);
    }


    @Test
    public void testBasicNgTemplating()  {
        List<RestService> services = SpringJavaRestReflector.reflectRestService(Arrays.asList(TestProbeController.class), true);
        TypescriptRestGenerator.generate(services, true);

        assertTrue(true);
    }

    @Test
    public void TestBasicRefaxctoring() {
        assertTrue(TinyRefactoringUtils.ngModuleAddDeclare("Booga", probe).indexOf("Booga") > 0);
    }
}
