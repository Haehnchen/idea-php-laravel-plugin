## 0.9
- Fix handling of namespace controller classes #31
- Add "uses" route references
- Add references for "Route::controller" controller names

## 0.7.1.2
- Template navigation improvements, also twig support for view render in php

## 0.7.1
- Fix multiple class resolve
- Fix non namespaces action completion #11
- Add new Laravel5 view resources path

## 0.7
- Add template namespace support #9
- Add support views() and config() of Laravel5
- Allow multiple template path and add Laravel4 and 5 default paths
- Add index for templates in php files and provide linemarker #15
- Enable better @include and @extends template navigation handling for the new features

## 0.6
- Fix "SqlStringLiteralExpression" import for popovers
- Add blade yield index and refactoring all other index element walkers
- Support blade yield directive in linemarker

## 0.5
- Add translation indexer and provider references "Lang" class alias
- Add config indexer and migrate references
- Porting completion autopopup Confidence from symfony2 plugin
- Fix route completion fired on every function

## 0.4
- Add blade sections implements linemarker
- Add blade sections references
- Provide some better blade file template completion
- Add linkAction references

## 0.3
- Add stub indexes for blade section, include and extends tags
- Add blade overwritten linemarker for sections
- Add file context include and extends linemarker
- Add textfield to configure default view namespace
- Only allow PhpStorm8 builds, for Blade support

## 0.2
- Add app config references
- Add Router::resource references

## 0.1
- Initial release
