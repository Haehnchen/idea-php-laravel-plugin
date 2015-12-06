<?php

namespace Illuminate\Contracts\Foundation
{
    class Application
    {
        public function singleton($i, $x) {}
    }
}

namespace Illuminate\Support {
    class ServiceProvider
    {
        /**
         * @var \Illuminate\Contracts\Foundation\Application
         */
        protected $app;
    }
}

namespace Foo
{
    use Illuminate\Support\ServiceProvider;

    class MyServiceProvider extends ServiceProvider
    {
        protected function registerFoo()
        {
            $this->app->singleton('foo', function ($app) {
                return new \DateTime();
            });

            $this->app->singleton('foo1', function ($app) {
                $dateTime = new \DateTime();
                return $dateTime;
            });
        }
    }
}