# ts-ng-tinydecorations Code Generator

This is bascially a set of intellij plugins in development
to support the [ts-ng-tinydecorations](https://github.com/werpu/ts-ng-tinydecorations) project.
and [Angular 5+](https://angular.io/).

It uses webpack in Angular 1.5+ (Tiny Decorations) and angular-cli for the Angular 5+ part.

Spring - Rest and JaxRS are supported (however JaxRS support is a little bit new and experimental, currently)

A temporary binary build can be found under

[codegen_plugin/build/distributions](https://github.com/werpu/tinydecscodegen/tree/master/codegen_plugin/build/distributions)

To install it simply install it via the install plugin from local filesystem
functionality of your Jetbrains ide.


## What can the plugin do

* Allows you to **generate a new** TinyDecorations (Annotated Angular 1.5+) or Angular 5 **project**.

* Generate Typescript classes from Java Dto Files (with annotation support for mapped implementation classes)
* Update existing generated typescript classes via the Intellij diff editor after
a successful code generation
* Generate Rest Clients from Spring and JaxRS Rest Endpoints
* Wizards for most of the Angular Artifacts supported by the Tiny Decorations project and Angular 5+
* Auto updating of associated mdules if a new artifact is generated
* Fallback option to use angular-cli for Angular 5+ projects
* Integrates seamlessly into angular-cli for Angular 5+ projects


## Is this plugin usable already

### Honest Answer

Yes definitely, I use it together with TinyDecorations and/or Angular 5+ in a bigger projects. 
So I am eating my own dogfood here. However the ui is bound to change, since it is very basic atm.
Also smaller bugs can be expected. 

However, since I currently develop
this project alone in my limited sparetime without any payment on it, I am glad that I do not have to many
users for this project atm, although I love people using my stuff, there is always support involved with it 
and this costs time I have to cut off from my family and implementation time. 

So feel free to use it I am happy about it, but I will keep it a little bit under the radar for the time being. 

The project simply was created because I was in the need of such tools
and hence developed it on my own for my needs. If you think something is missing or if you want to donate
code feel free to send me a code drop/pull request or add a feature request in the projects list.

Hence there is no offical drop of the plugin into the Jetbrains repository.

More info on how to use it fill follow soon, depending on my time

