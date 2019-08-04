/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JSExistsRule implements TestRule {

    @Override
    public Statement apply(Statement statement, Description description) {
        JSInSystem annotation = description.getAnnotation(JSInSystem.class);
        if (annotation == null) {
            return statement;
        }
        String testDataSet = System.getProperty("dataset");

        try {
            Class.forName("com.intellij.lang.typescript.psi.TypeScriptExternalModuleReference");
            return statement;
        } catch (ClassNotFoundException e) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    // Return an empty Statement object for those tests
                    // that shouldn't run on the specified dataset.
                }
            };
        }


    }
}
