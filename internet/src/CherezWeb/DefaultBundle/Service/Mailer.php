<?php

namespace CherezWeb\DefaultBundle\Service;
use Symfony\Bundle\TwigBundle\TwigEngine;

class Mailer {
    private $fromName;
    private $fromEmail;
    private $mailer;
    private $instantMailer;
    private $templating;
    
    public function __construct(\Swift_Mailer $mailer, \Swift_Mailer $instantMailer, TwigEngine $templating, $fromEmail, $fromName) {
        $this->mailer        = $mailer;
        $this->instantMailer = $instantMailer;
        $this->templating    = $templating;
        $this->fromEmail     = $fromEmail;
        $this->fromName      = $fromName;
    }
    
    public function sendMail($subject, $to, $viewWithoutExtension, array $viewParameters = array()) {
        $this->mailer->send($this->buildMessage($subject, $to, $viewWithoutExtension, $viewParameters));
    }
    
    public function sendInstantMail($subject, $to, $viewWithoutExtension, array $viewParameters = array()) {
        $this->instantMailer->send($this->buildMessage($subject, $to, $viewWithoutExtension, $viewParameters));
    }
    
    protected function buildMessage($subject, $to, $viewWithoutExtension, array $viewParameters = array()) {
        return \Swift_Message::newInstance()
            ->setSubject($subject)
            ->setFrom(array($this->fromEmail => $this->fromName))
            ->setTo($to)
            ->setBody($this->renderView($viewWithoutExtension.'.html.twig', $viewParameters),'text/html')
            ->addPart($this->renderView($viewWithoutExtension.'.text.twig', $viewParameters),'text/plain');
    }
    
    /**
     * Returns a rendered view.
     *
     * @param string $view       The view name
     * @param array  $parameters An array of parameters to pass to the view
     *
     * @return string The rendered view
     */
    protected function renderView($view, array $parameters = array()) {
        return $this->templating->render($view, $parameters);
    }
    
}
