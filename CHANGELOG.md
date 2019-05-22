# Changelog

## Versions
* 0.15.x: PhpStorm 2017.3.2+

## 0.15.4
* Java 8 build

## 0.15.3
* Add IntelliJ plugin icon (Daniel Espendiller) [#228](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/228)
* feat: added completion for laravel dusk (Viktor Vassilyev) [#226](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/226)
* feat: added completion if Route::match (Viktor Vassilyev) [#225](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/225)
* Compile Patterns before repeated use (Martynas Sateika) [#223](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/223)
* Remove unnecessary KeyDescriptor instances from index files (Martynas Sateika) [#224](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/224)
* Use Collections.singleton to avoid new HashSet creation (Martynas Sateika) [#222](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/222)
* Replace 'substring' with 'trimStart' from the OpenAPI utils (Martynas Sateika) [#221](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/221)
* Re-use ConfigFileMatchResult instance when no match is found (Martynas Sateika) [#220](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/220)
* Move new ArrayList creation until after null check in 'getChildrenFix' (Martynas Sateika) [#219](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/219)

## 0.15.2
* Require Project to be @NotNull on LaravelSettings#getInstance (Cedric Ziel)
* Dont offer ExtractPartialViewAction when no project on event (Cedric Ziel)

## 0.15
* Fix typo in settings form @Luiz* [#160](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/160</)
* Replace deprecated ScalarIndexExtension usages in indexing process
* Support Blade includeWhen, includeFirst for template references [#164](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/164) [#152](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/152) [#158](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/158) [#142](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/142)
* Use new directory SLOT\_DIRECTIVE and COMPONENT\_DIRECTIVE for completion parameter pattern [#167](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/167)
* Provide indexer for all Blade include directives and use some new Blade apis [#165](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/165)
* Replace cost intensive template resolving on path visiting
* Convert Blade template file navigation to lazy target implementation
* Convert Blade template include navigation to lazy target implementation
* Config files matching refactored + tests added @Adel [#169](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/169) [#166](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/166)
* Fix "component" and "slot" navigation
* Provide directory navigation for Blade template strings
* Fix performance issue for Blade linemarkers: targets must be attached to leaf elements [#161](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/161)
* Provide references for assets [#170](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/170)

## 0.14.2
* Additional check for Route::resource analyzer [#130](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/130) [#131](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/131) @adelf
* Fixed duplicates in @lang blade directive [#133](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/133) @adelf
* Feature prioritized lang goto [#134](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/134) @adelf

## 0.14.1
* Add TypeProvider for Blade @inject Directive [#128](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/128)
* Implement PhpTypeProvider3 for type resolving [#78](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/78)
* Support class constant of provider alias
* Support subdirectories of routes definitions [#129](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/129) [#69](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/69)
    
## 0.14
* Support for __() and trans_choice() [#124](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/124) [#125](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/125) @adelf
* Fix incorrect route name generation for Route::resource [#119](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/119) @adelf
* Add view references for the Mailer Class [#126](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/126) @diogogomeswww
* PhpStorm 2017.1 migration for new directive support in Blade views
* Add linemaker for Blade slot overwrites inside components

## 0.13.2
* Replace deprecated api usages
* Prevent duplicates on blade templates Related files [#112](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/112) @adelf
* Remove index calling in RouteGroupUtil. Fixes IndexNotReadyException [#108](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/108), [#109](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/109) @adelf
* Route::resource route names support [#113](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/113) @adelf

## 0.13.1
* Fix navigation to declaration doesn't work when @component has the optional array parameter [#105](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/105)
* Fix route controller/resource completion [#106](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/106) [#107](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/107) @adelf

## 0.13
* Extract to template file [#102](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/102) [#23](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/23) @adelf
* Add index for custom Blade directives abd provide completion [#103](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/103) [#88](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/88) @adelf
* Partial namespaced controllers [#95](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/95) @adelf

## 0.12.4
* Fix thread exception in template settings forms [#99](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/99)

## 0.12.3
* Add more pattern and language validation for completion; fix npe [#96](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/96)

## 0.12.2
* Fix npe in ControllerReferences [#92](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/92) [#90](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/90) [#93](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/93) @adelf
* Support Laravel 5.4 blade component directive [#91](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/91)

## 0.12.1
* Support template "each" directive [#86](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/86)
* Add Blade "inject" directive for service injection [#87](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/87)
* Add Blade "includeIf" directive template references [#89](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/89)

## 0.12
* Remove AuthorizesRequests trait methods [#52](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/52)
* Add references for Blade @push @stack directives [#73](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/73)
* Add PhpStorm environments up to 2016.3.2 for travis testing
* Add dialog for automatically detect the need to enable plugin [#80](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/80)

## 0.11
* Move settings form into PHP related menu [#63](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/63)
* Support route file structure of Laravel 5.3 and include route name index instead in of live extract "as" routes [#69](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/69)
* Drop project usage for LaravelSettings and make it stateless to prevent memory leaks; cleanup unused code of LaravelProjectSettingsForm
* Fix npe in ControllerReferences [#66](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/66)
* Fix locating translations doesn't seem to work when using a locale which is not an official locale; support "fr-FR" [#58](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/58)
* Travis testing environment update for PhpStorm 2016.2.1
* Fix settings for template path reset was not filled

## 0.11
* Fix Plugin throws java.util.ConcurrentModificationException [#64](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/64)

## 0.10.0
* Dont print full function signature in tail text, only extract parameter
* Dropping redundant view path setting elements [#59](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/59)
* Route::group old laravel versions support [#47](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/47)
* Fix MethodReferenceImpl cannot be cast to ... in dic type provider [#54](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/54) [#60](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/60)
* Update travis environment to use PhpStorm.2016+ and Java8 deps
* Fix jump to doesn't work for @include('some/view/path') [#62](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/62)
* Support translate in subdirectories [#61](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/61)

## 0.9.1
* Fix Route::group and Laravel doesn't allow Route::group for controllers not in default namespace [#46](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/46) @adelf
* Route completion displays origin class of trait methods and provides parameter tail text

## 0.9
* Better default router namespace detection; also add settings for customize [#35](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/35)
* Non-standard root namespaces support @adelf
* Fixing npe in ControllerReferences [#40](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/40)
* Route::group support @adelf [#8](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/8)
* Prioritized controller completion inside groups @adelf
* fix navigation to view in custom view folder without namespace problem [#30](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/30)
* Only try optional view path on settings form [#24](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/24)
* Add support for naming routes [#21](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/21), [#34](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/34)
* Add blade @lang directive support [#19](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/19)
* Add service dic support [#42](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/42)

## 0.8
* Fix handling of namespace controller classes [#31](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/31)
* Add "uses" route references
* Add references for "Route::controller" controller names

## 0.7.2
* Template navigation improvements, also twig support for view render in php

## 0.7.1
* Fix multiple class resolve
* Fix non namespaces action completion [#11](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/11)
* Add new Laravel5 view resources path

## 0.7
* Add template namespace support [#9](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/9)
* Add support views() and config() of Laravel5
* Allow multiple template path and add Laravel4 and 5 default paths
* Add index for templates in php files and provide linemarker [#15](https://github.com/Haehnchen/idea-php-laravel-plugin/issues/15)
* Enable better @include and @extends template navigation handling for the new features

## 0.6
* Fix "SqlStringLiteralExpression" import for popovers
* Add blade yield index and refactoring all other index element walkers
* Support blade yield directive in linemarker

## 0.5
* Add translation indexer and provider references "Lang" class alias
* Add config indexer and migrate references
* Porting completion autopopup Confidence from symfony2 plugin
* Fix route completion fired on every function

## 0.4
* Add blade sections implements linemarker
* Add blade sections references
* Provide some better blade file template completion
* Add linkAction references

## 0.3
* Add stub indexes for blade section, include and extends tags
* Add blade overwritten linemarker for sections
* Add file context include and extends linemarker
* Add textfield to configure default view namespace
* Only allow PhpStorm8 builds, for Blade support

## 0.2
* Add app config references
* Add Router::resource references

## 0.1
* initial release