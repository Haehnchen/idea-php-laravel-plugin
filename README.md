IntelliJ IDEA / PhpStorm Laravel Plugin
-------------
[![Build Status](https://travis-ci.org/Haehnchen/idea-php-laravel-plugin.svg?branch=master)](https://travis-ci.org/Haehnchen/idea-php-laravel-plugin)
[![Version](http://phpstorm.espend.de/badge/7532/version)](https://plugins.jetbrains.com/plugin/7532)
[![Downloads](http://phpstorm.espend.de/badge/7532/downloads)](https://plugins.jetbrains.com/plugin/7532)
[![Downloads last month](http://phpstorm.espend.de/badge/7532/last-month)](https://plugins.jetbrains.com/plugin/7532)
[![Donate to this project using Paypal](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.me/DanielEspendiller)

Based on [Symfony Plugin](https://github.com/Haehnchen/idea-php-symfony2-plugin)

Key         | Value
----------- | -----------
Plugin url  | https://plugins.jetbrains.com/plugin/7532
Id          | de.espend.idea.laravel
Changelog   | [CHANGELOG](CHANGELOG.md)

## Installation

To install,  go to `Settings > Plugins` and search for "Laravel Plugin".

Once installed, you must activate per-project by going to `Settings > Languages & Frameworks > PHP > Laravel` and clicking "Enable for this project".

*Note* You must install and use the [Laravel IDE Helper](https://github.com/barryvdh/laravel-ide-helper) in order for PhpStorm to know how to find the Laravel classes.

## Documentation and tutorials

 * JetBrains: [Laravel Development using PhpStorm](https://confluence.jetbrains.com/display/PhpStorm/Laravel+Development+using+PhpStorm)

### Blade Template Namespace

To register custom Blade template paths use `ide-blade.json` files in directories which template related.
`Path` need to be relative to its file position  

```json
{
  "namespaces": [
    {
      "namespace": "foo",
      "path": "res"
    },
    {
      "namespace": "foo"
    }
  ]
}
```
Make sure that the namespace does not have dot `.` delimiter, because the plugin will not work in that situation.
For example, if the above example had namespace called `foo.bar` then it will not work correctly.

## Screenshots

![larevel phpstorm](http://plugins.jetbrains.com/files/7532/screenshot_14670.png)

