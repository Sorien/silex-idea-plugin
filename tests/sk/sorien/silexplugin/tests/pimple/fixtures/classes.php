<?php
namespace Sorien {
    class Application extends \Silex\Application {
    }
}

namespace Silex {
    class Application extends \Pimple\Container {
    }
}

namespace Pimple {

    class Container {

        public function __construct(array $values = array())
        {
        }

        public function extend($id, $callable)
        {
        }

        public function raw($id)
        {
        }

        public function protect($callable)
        {
        }

        public function factory($callable)
        {
        }
    }
}