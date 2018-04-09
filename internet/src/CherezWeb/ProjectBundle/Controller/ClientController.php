<?php

namespace CherezWeb\ProjectBundle\Controller;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use CherezWeb\ProjectBundle\Entity\Channel;
use CherezWeb\ProjectBundle\Entity\Client;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use CherezWeb\ProjectBundle\Service\ChannelAccessManager;

class ClientController extends Controller
{

    /**
     * @Security("is_authenticated()")
     */
    public function listAction()
    {
        $clients = $this->getDoctrine()->getRepository('CherezWebProjectBundle:Client')
            ->findAllByUser($this->getUser());
        
        $channels = $this->getDoctrine()->getRepository('CherezWebProjectBundle:Channel')
            ->findAllByUser($this->getUser());
        return $this->render(
                'CherezWebProjectBundle:Client:list.html.twig', [
                'clients' => $clients,
                'channels' => $channels,
                ]
        );
    }

    /**
     * @Security("is_authenticated()")
     */
    public function createAction(Request $request)
    {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $keyManager = $this->get('cherez_web.project.key_manager');
        /* @var $keyManager \CherezWeb\ProjectBundle\Service\KeyManager */

        $client = new Client;
        $client->setKey($keyManager->generateUniqueKey());
        $client->setUser($this->getUser());

        $editForm = $this->createForm(
            new \CherezWeb\ProjectBundle\Form\ClientEditType, $client, array('action' => $request->getUri())
        );
        $editForm->handleRequest($request);

        if ($editForm->isValid()) {
            $em->persist($client);
            $em->flush();
            $this->get('session')->getFlashBag()->add(
                'notice_success', sprintf('Новый видео-клиент добавлен.')
            );
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
        }

        return $this->render('CherezWebProjectBundle:Client:create.html.twig', array(
                'form' => $editForm->createView(),
        ));
    }

    /**
     * @Security("is_granted('edit', client)")
     */
    public function editAction(Client $client, Request $request)
    {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $editForm = $this->createForm(
            new \CherezWeb\ProjectBundle\Form\ClientEditType, $client, array('action' => $request->getUri())
        );
        $editForm->handleRequest($request);

        if ($editForm->isValid()) {
            $em->flush();
            $this->get('session')->getFlashBag()->add(
                'notice_success', sprintf('Видео-клиент #%s отредактирован.', $client->getId())
            );
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
        }

        return $this->render('CherezWebProjectBundle:Client:edit.html.twig', array(
                'form' => $editForm->createView(),
        ));
    }

    /**
     * @Security("is_granted('edit', client)")
     */
    public function updateKeyAction(Client $client)
    {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $keyManager = $this->get('cherez_web.project.key_manager');
        /* @var $keyManager \CherezWeb\ProjectBundle\Service\KeyManager */

        $client->setKey($keyManager->generateUniqueKey());
        $em->flush();

        $this->get('session')->getFlashBag()->add(
            'notice_success', sprintf('Ключ доступа изменен для клиента #%s.', $client->getId())
        );
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }

    /**
     * @Security("is_granted('edit', client)")
     */
    public function deleteAction(Client $client, Request $request)
    {
        $confirmed = (bool) $request->get('confirmed', FALSE);

        if ($confirmed === FALSE) {
            return $this->render('CherezWebProjectBundle:Client:delete.html.twig', [
                    'client' => $client
            ]);
        }

        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $deletedId = $client->getId();
        $deletedTitle = $client->getTitle();

        $em->remove($client);
        $em->flush();

        $this->get('session')->getFlashBag()->add(
            'notice_success', sprintf('Видео-клиент #%s (%s) удален.', $deletedId, $deletedTitle)
        );
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }

    /**
     * @Security("is_granted('edit', client) and is_granted('edit', channel)")
     */
    public function addAccessAction(Client $client, Channel $channel)
    {
        $channelAccessManager = $this->get('cherez_web.project.channel_access_manager');
        /* @var $channelAccessManager ChannelAccessManager */
        
        $channelAccessManager->addAccess($channel, $client);
        
        $client->updateStateUid();
        $this->getDoctrine()->getManager()->flush();
        
        $this->get('session')->getFlashBag()->add(
            'notice_success',
            sprintf(
                'Видео-клиенту "%s" разрешен доступ к каналу "%s".',
                $client->getTitle(),
                $channel->getTitle()
            )
        );
        
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }
    
    /**
     * @Security("is_granted('edit', client) and is_granted('edit', channel)")
     */
    public function removeAccessAction(Client $client, Channel $channel)
    {
        $channelAccessManager = $this->get('cherez_web.project.channel_access_manager');
        /* @var $channelAccessManager ChannelAccessManager */
        
        $channelAccessManager->removeAccess($channel, $client);
        
        $client->updateStateUid();
        $this->getDoctrine()->getManager()->flush();
        
        $this->get('session')->getFlashBag()->add(
            'notice_success',
            sprintf(
                'Видео-клиенту "%s" запрещен доступ к каналу "%s".',
                $client->getTitle(),
                $channel->getTitle()
            )
        );
        
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }
}
