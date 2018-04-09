<?php

namespace CherezWeb\ProjectBundle\DependencyInjection\Compiler;

use Symfony\Component\DependencyInjection\ContainerBuilder;
use Symfony\Component\DependencyInjection\Compiler\CompilerPassInterface;
use Symfony\Component\DependencyInjection\Reference;

class ApiCommandProcessorCompilerPass implements CompilerPassInterface {

    public function process(ContainerBuilder $container) {
        if (!$container->has('cherez_web.project.app_command_manager')) {
            return;
        }

        $definition = $container->findDefinition(
                'cherez_web.project.app_command_manager'
        );

        $taggedServices = $container->findTaggedServiceIds(
                'cherez_web.project.app_command_processor'
        );
        foreach ($taggedServices as $id => $tags) {
            $definition->addMethodCall(
                'addProcessor',
                array(new Reference($id))
            );
        }
    }

}
