<?php

namespace CherezWeb\ProjectBundle\Command;

use Doctrine\ORM\EntityManager;
use Symfony\Bundle\FrameworkBundle\Command\ContainerAwareCommand;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

class RequestsCleanupCommand extends ContainerAwareCommand
{

    protected function configure()
    {
        $this
            ->setName('cherez_web:project:requests_cleanup')
            ->setDescription('Cleanup olg requests and responses.');
    }

    protected function execute(InputInterface $input, OutputInterface $output)
    {
        $em = $this->getContainer()->get('doctrine')->getEntityManager();
        /* @var $em EntityManager */
        
        $limit = (new \DateTime)->sub(new \DateInterval('PT40S'));
        
        $em->getRepository('CherezWebProjectBundle:ChannelRequest')
            ->createQueryBuilder('creq')
            ->delete()
            ->where('creq.updated <= :limit')
            ->setParameter('limit', $limit, 'utcdatetime')
            ->getQuery()
            ->execute();

        $em->flush();
        
        $output->writeln('done');
    }
}
