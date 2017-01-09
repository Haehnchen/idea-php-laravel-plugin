## 0.12.2
* Fix npe in ControllerReferences [#92](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/92) [#90](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/90) [#93](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/93) @adelf
* Support Laravel 5.4 blade component directive [#91](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/91)

## 0.12.1
* Support template "each" directive [#86](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/86)
* Add Blade "inject" directive for service injection [#87](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/87)
* Add Blade "includeIf" directive template references [#89](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/89)

## 0.12
* Remove AuthorizesRequests trait methods [#52](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/52)
* Add references for Blade @push @stack directives [#73](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/73)
* Add PhpStorm environments up to 2016.3.2 for travis testing
* Add dialog for automatically detect the need to enable plugin [#80](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/80)

## 0.11
* Move settings form into PHP related menu [#63](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/63)
* Support route file structure of Laravel 5.3 and include route name index instead in of live extract "as" routes [#69](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/69)
* Drop project usage for LaravelSettings and make it stateless to prevent memory leaks; cleanup unused code of LaravelProjectSettingsForm
* Fix npe in ControllerReferences [#66](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/66)
* Fix locating translations doesn't seem to work when using a locale which is not an official locale; support "fr-FR" [#58](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/58)
* Travis testing environment update for PhpStorm 2016.2.1
* Fix settings for template path reset was not filled

## 0.11
* Fix Plugin throws java.util.ConcurrentModificationException [#64](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/64)

## 0.10.0
* Dont print full function signature in tail text, only extract parameter
* Dropping redundant view path setting elements [#59](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/59)
* Route::group old laravel versions support [#47](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/47)
* Fix MethodReferenceImpl cannot be cast to ... in dic type provider [#54](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/54) [#60](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/60)
* Update travis environment to use PhpStorm.2016+ and Java8 deps
* Fix jump to doesn't work for @include('some/view/path') [#62](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/62)
* Support translate in subdirectories [#61](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/61)

## 0.9.1
* Fix Route::group and Laravel doesn't allow Route::group for controllers not in default namespace [#46](https://github.com/Haehnchen/idea-php-laravel-plugin/pull/46) @adelf
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