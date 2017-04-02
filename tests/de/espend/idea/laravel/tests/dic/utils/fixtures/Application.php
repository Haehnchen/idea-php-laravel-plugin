<?php

namespace Illuminate\Foundation;

class Application
{
    public function registerCoreContainerAliases()
    {
        $aliases = [
            'app' => ['Illuminate\Foundation\Application', 'Illuminate\Contracts\Container\Container', 'Illuminate\Contracts\Foundation\Application'],
            'auth' => 'Illuminate\Auth\AuthManager',
            'auth.driver' => ['Illuminate\Auth\Guard', 'Illuminate\Contracts\Auth\Guard'],
            'auth.password.tokens' => '\Illuminate\Auth\Passwords\TokenRepositoryInterface',
            'blade.compiler' => 'Illuminate\View\Compilers\BladeCompiler',
            'blade.compiler_class' => \Illuminate\View\Compilers\BladeCompiler::class,
            'blade.compiler_class_array' => [\Illuminate\View\Compilers\BladeCompiler::class],
            'blade.compiler_foo' => null,
        ];
    }
}
