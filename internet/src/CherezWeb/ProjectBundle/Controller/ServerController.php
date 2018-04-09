<?php

namespace CherezWeb\ProjectBundle\Controller;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use CherezWeb\ProjectBundle\Entity\Server;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;

class ServerController extends Controller
{

    /**
     * @Security("is_authenticated()")
     */
    public function listAction()
    {
        $servers = $this->getDoctrine()->getRepository('CherezWebProjectBundle:Server')
            ->findAllByUser($this->getUser());
        return $this->render(
                'CherezWebProjectBundle:Server:list.html.twig', [
                'servers' => $servers
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

        $server = new Server;
        $server->setKey($keyManager->generateUniqueKey());
        $server->setUser($this->getUser());

        $editForm = $this->createForm(
            new \CherezWeb\ProjectBundle\Form\ServerEditType, $server, array('action' => $request->getUri())
        );
        $editForm->handleRequest($request);

        if ($editForm->isValid()) {
            $em->persist($server);
            $em->flush();
            $this->get('session')->getFlashBag()->add(
                'notice_success', sprintf('Новый видео-сервер добавлен.')
            );
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
        }

        return $this->render('CherezWebProjectBundle:Server:create.html.twig', array(
                'form' => $editForm->createView(),
        ));
    }

    /**
     * @Security("is_granted('edit', server)")
     */
    public function editAction(Server $server, Request $request)
    {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $editForm = $this->createForm(
            new \CherezWeb\ProjectBundle\Form\ServerEditType, $server, array('action' => $request->getUri())
        );
        $editForm->handleRequest($request);

        if ($editForm->isValid()) {
            $em->flush();
            $this->get('session')->getFlashBag()->add(
                'notice_success', sprintf('Видео-сервер #%s отредактирован.', $server->getId())
            );
            return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
        }

        return $this->render('CherezWebProjectBundle:Server:edit.html.twig', array(
                'form' => $editForm->createView(),
        ));
    }

    /**
     * @Security("is_granted('edit', server)")
     */
    public function updateKeyAction(Server $server)
    {
        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $keyManager = $this->get('cherez_web.project.key_manager');
        /* @var $keyManager \CherezWeb\ProjectBundle\Service\KeyManager */

        $server->setKey($keyManager->generateUniqueKey());
        $em->flush();

        $this->get('session')->getFlashBag()->add(
            'notice_success', sprintf('Ключ доступа изменен для сервера #%s.', $server->getId())
        );
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }

    /**
     * @Security("is_granted('edit', server)")
     */
    public function deleteAction(Server $server, Request $request)
    {
        $confirmed = (bool) $request->get('confirmed', FALSE);

        if ($confirmed === FALSE) {
            return $this->render('CherezWebProjectBundle:Server:delete.html.twig', [
                    'server' => $server
            ]);
        }

        $em = $this->getDoctrine()->getManager();
        /* @var $em \Doctrine\ORM\EntityManager */

        $deletedId = $server->getId();
        $deletedLocation = $server->getLocation();

        $em->remove($server);
        $em->flush();

        $this->get('session')->getFlashBag()->add(
            'notice_success', sprintf('Видео-сервер #%s (%s) удален.', $deletedId, $deletedLocation)
        );
        return $this->render('CherezWebDefaultBundle:AjaxResponse:redirect.html.twig');
    }
}
