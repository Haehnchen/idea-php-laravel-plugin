<?php

namespace {
    Route::get('user/profile', ['as' => 'profile']);
}


namespace Illuminate\Routing {
    interface UrlGenerator {
        public function route();
    }
}

namespace Illuminate\Contracts\Routing {
    interface UrlGenerator {
        public function route();
    }
}

namespace Collective\Html {
    interface HtmlBuilder {
        public function linkRoute();
    }
}


namespace Laravel\BrowserKitTesting\Concerns {
    trait InteractsWithPages {
        /**
         * @param  string  $route
         */
        public function visitRoute($route){
            return '';
        }
    }

}
namespace Tests\Feature {
  class ExampleTest {
      use \Laravel\BrowserKitTesting\Concerns\InteractsWithPages;
  }
}