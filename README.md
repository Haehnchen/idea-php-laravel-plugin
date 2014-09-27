Laravel Plugin for PHPStorm
-------------

## Links
 * Plugin url: http://plugins.jetbrains.com/plugin/7532

## Installation

To install,  go to `Settings > Plugins` and search for "Laravel Plugin".

Once installed, you must activate per-project by going to `Settings > Laravel Plugin` and clicking "Enable for this project".

*Note* Currently, you must install and use the [Laravel IDE Helper](https://github.com/barryvdh/laravel-ide-helper) in order for PHPStorm to know how to find the Laravel classes.

## Current Features
 * PHP/Route: Controller completion and goto
 * PHP/Route: Router::resource references
 * PHP/View: completion and goto for view templates
 * PHP/Config: "providers" class array completion
 * PHP/Config: Config key completion and goto
 * Blade: extends and include linemarker
 * Blade: section overwrite and implements linemarker
 * Blade: Improvements in blade template name completion

## TODO
 * Try to remove "IDE Helper Generator" deps
 * Require Symfony2 Plugin, to reduce duplicate code
 * Support multiple view namespaces
 * Support trans/transChoice translation syntax
 * Support yield directive in blade

## Screenshots

![alt text](http://plugins.jetbrains.com/files/7532/screenshot_14670.png)

