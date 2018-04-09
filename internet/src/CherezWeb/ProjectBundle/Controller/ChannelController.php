<?php

namespace CherezWeb\ProjectBundle\Controller;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use CherezWeb\ProjectBundle\Entity\Channel;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;

class ChannelController extends Controller
{

    /**
     * @Security("is_authenticated()")
     */
    public function listAction()
    {
        $channels = $this->getDoctrine()->getRepository('CherezWebProjectBundle:Channel')
            ->findAllByUser($this->getUser());
        return $this->render(
                'CherezWebProjectBundle:Channel:list.html.twig', [
                'channels' => $channels
                ]
        );
    }

    /**
     * @Security("is_granted('edit', channel)")
     */
    public function editAction(Channel $channel, Request $request)
    {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $editForm = $this->createForm(
            new \CherezWeb\ProjectBundle\Form\ChannelEditType, $channel, array('action' => $request->getUri())
        );
        $editForm->handleRequest($request);

        if ($editForm->isValid()) {
            foreach ($channel->getAccesses() as $access) {
                /* @var $access \CherezWeb\ProjectBundle\Entity\ChannelAccess */
                $access->getClient()->updateStateUid();
            }
            $em->flush();
            $this->get('session')->getFlashBag()->add(
                'notice_success', sprintf('Видео-клиент #%s отредактирован.', $channel->getId())
            );
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
        }

        return $this->render('CherezWebProjectBundle:Channel:edit.html.twig', array(
                'form' => $editForm->createView(),
        ));
    }

    /**
     * @Security("is_granted('edit', channel)")
     */
    public function changeOrderAction(Channel $channel, $order)
    {
        /* @var $channels \Doctrine\Common\Collections\ArrayCollection */
        $channels = $this->getDoctrine()
            ->getRepository('CherezWebProjectBundle:Channel')
            ->getQbFindAllByUser($this->getUser())
            ->andWhere('c != :channel')
            ->setParameter('channel', $channel)
            ->getQuery()
            ->getResult();

        /*
         * Если указана позиция за границами диапазона, то в качестве значения
         * берем ближайшее крайнее значение.
         */
        $safeOrder = min(max($order, 0), count($channels) + 1);

        // Заранее проставляем позицию для изменяемой страницы.
        $channel->setOrder($safeOrder);

        $currentIndex = 0;
        /* @var $item Channel */
        foreach ($channels as $item) {
            if ($safeOrder == $currentIndex) {
                $currentIndex++;
            }
            $item->setOrder($currentIndex);
            $currentIndex++;
        }
        
        foreach ($channels as $channel) {
            /* @var $channel Channel */
            foreach ($channel->getAccesses() as $access) {
                /* @var $access \CherezWeb\ProjectBundle\Entity\ChannelAccess */
                $access->getClient()->updateStateUid();
            }
        }

        $this->getDoctrine()->getManager()->flush();


        $this->get('session')->getFlashBag()->add(
            'notice_success', 'Порядок страниц успешно изменен'
        );
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }
}
