# ts-ng-tinydecorations Code Generator

![Plugin in Action](https://github.com/werpu/tinydecscodegen/blob/master/docs/images/create_component.jpg)

This is bascially a set of Intellij plugins in development
to support the AngularJS based [ts-ng-tinydecorations](https://github.com/werpu/ts-ng-tinydecorations) project.
and [Angular](https://angular.io/).

It uses webpack in AngularJS 1.5+ (Tiny Decorations) and angular-cli for the Angular 5+ part.

Spring - Rest and JaxRS are supported (however JaxRS support is a little bit new and experimental, currently)

A temporary binary build can be found under

[codegen_plugin/build/distributions](https://github.com/werpu/tinydecscodegen/tree/master/codegen_plugin/build/distributions)

To install it, simply use the **Install Plugin from Local Filesystem**
functionality of your Jetbrains IDE.


## Are there any final releases already?

Yes at the time of writing this, the 1.0 release has been tagged
please go to https://github.com/werpu/tinydecscodegen/releases/tag/1.0.0-FINAL
for the sources and binaries.


## What can the Plugin do

Latest Code:

Following additional features are present at the latest development build:

* Angular/AngularJS resource views and resource search
* Improved Angular tooling and support (main development still is on Angular JS)
* I18N refactorings
* Tools to migrate old code typescript to annotations 
* Lots of bugfixes
* Support for the latest IDEA/Webstorm releases

1.0

* Allows you to **generate a new** TinyDecorations (Annotated AngularJS 1.5+) or Angular 5 **project**.

* Generate Typescript classes from Java Dto Files (with annotation support for mapped implementation classes)
* Update existing generated Typescript classes via the Intellij diff editor after
a successful code generation
* Generate Rest clients from Spring and JaxRS Rest endpoints
* Easy generation of Rest endpoints
* Wizards for most of the Angular Artifacts supported by the Tiny Decorations project and Angular 5+
* Auto updating of associated modules once a new artifact is generated
* Fallback option to use angular-cli for Angular 5+ projects
* Integrates seamlessly into angular-cli for Angular 5+ projects
* Navigational Tree for easy navigation within your project
* Navigational helpers to easily jump between java Dtos/Rest services and their Typescript counterparts




## Are there Demo Videos?

Yes, there is a comprehensive video guide for version 1.0, currently for AngularJS only
(but it works the same for Angular 5+).

[![Link to Youtube](https://github.com/werpu/tinydecscodegen/blob/master/docs/images/youtube_vid.jpg)](https://www.youtube.com/watch?v=MvJY0z3oIYk&list=PLNRFvroappqZZKSrCGBwOSqb-pLomytw6&index=1).

### Subchapters if you do not want to watch everything
 
* Part 1: [Getting started, project setup](https://youtu.be/MvJY0z3oIYk)
* Part 2: [Creating Views/Controllers](https://youtu.be/-vPs09igAvM)
* Part 2.1: [Creating Views - additional information](https://youtu.be/aO7XTnmyXG4)
* Part 3: [Component Creation](https://youtu.be/PWfJS6vbd-0)
* Part 4: [Services and Rest Calls](https://youtu.be/HnHf_lfe2BY)
   

## Is this Plugin Usable Already?

### Honest Answer

Yes definitely, I use it together with TinyDecorations and/or Angular 5+ in a bigger project. 
So I am eating my own dogfood here. And develop what I need on a day to day base.
But smaller bugs can be expected, and feedback and bugreports or patchs are always appreciated.

However, since I currently develop
this project alone in my limited sparetime without any payment, I am glad that I do not have too many
users for this project atm. Although I love people using my stuff, there is always support involved with it 
and this costs time, which I have to cut off from my family and implementation time. 

Hence, there is no official drop of the plugin into the Jetbrains repository at the moment.

So feel free to use it. I am happy about it, but never mind, that I cannot give
full blown professional support for the time being. I will answer and give
support as good as possible, however.

The project simply was created because I was in need of such tools
and hence developed it on my own for my needs. If you think something is missing or if you want to donate
code, feel free to send me a code drop/pull request or add a feature request in the projects list.

I might open a patreon in the future for small money donations, but ATM I wont,
because I shun a little bit the long term responsibility which comes with it, timewise.



## Links

* [Wiki](https://github.com/werpu/tinydecscodegen/wiki)
* [ts-ng-tinydecorations](https://github.com/werpu/ts-ng-tinydecorations) 
* [Angular 5+](https://angular.io/)
* [Link to Youtube playlist hosting the demo and tutorial videos](https://www.youtube.com/watch?v=GNpvAFgr1rw&list=PLNRFvroappqZZKSrCGBwOSqb-pLomytw6)