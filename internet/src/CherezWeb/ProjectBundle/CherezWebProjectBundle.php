<?php

namespace CherezWeb\ProjectBundle;

use CherezWeb\ProjectBundle\DependencyInjection\Compiler\ApiCommandProcessorCompilerPass;
use Symfony\Component\DependencyInjection\ContainerBuilder;
use Symfony\Component\HttpKernel\Bundle\Bundle;

class CherezWebProjectBundle extends Bundle
{

    public function build(ContainerBuilder $container)
    {
        parent::build($container);

        $container->addCompilerPass(new ApiCommandProcessorCompilerPass());
    }
}
