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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Small main class to run the entire temlplating from the command line
 */
public class Main {

    public static void main(String... argv) {

        //args all classes to analyze with their corresponding classpath

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        List<Class> classes = (List<Class>) Arrays.asList(argv).stream().map(s -> {
            try {
                return Optional.of(cl.loadClass(s));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return Optional.ofNullable(null);
            }
        }).filter(cls -> {
            if (cls.isPresent()) {
                return true;
            }
            return false;
        }).map(opt -> {
            return opt.get();
        }).collect(Collectors.toList());



    }

}
