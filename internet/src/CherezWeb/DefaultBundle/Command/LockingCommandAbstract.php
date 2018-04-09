<?php

namespace CherezWeb\DefaultBundle\Command;

use Symfony\Bundle\FrameworkBundle\Command\ContainerAwareCommand;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

abstract class LockingCommandAbstract extends ContainerAwareCommand {
    
    abstract protected function executeWithLocking(InputInterface $input, OutputInterface $output);
    
    final protected function execute(InputInterface $input, OutputInterface $output) {
        set_error_handler(function ($errno, $errstr, $errfile, $errline ) {
            throw new \ErrorException($errstr, 0, $errno, $errfile, $errline);
        });
        set_time_limit($this->getMaxExecutionTime());
        $executionLock = $this->getExecutionLock();
        if ($executionLock !== NULL) {
            $output->writeln('Process is runing');
            $start = $executionLock['start'];
            $pid = $executionLock['pid'];
            $now = time();
            $execution = $now - $start;
            if ($start < $now && $execution > $this->getMaxExecutionTime()) {
                // Процесс уже не работает, а блокировка осталась.
                $output->writeln('Lock is out of date' . PHP_EOL);
                $this->deleteExecutionLock();
            } else {
                $output->writeln('Start time: ' . date("Y-m-d H:i:s", $start));
                $output->writeln('Execution time, min.: ' . $execution / 60);
                $output->writeln('Max execution time, min.: ' . $this->getMaxExecutionTime() / 60);
                $output->writeln('Max execution time PHP, min.: ' . ini_get('max_execution_time') / 60);
                return;
            }
        }
        
        $this->createExecutionLock();
        $this->executeWithLocking($input, $output);
        $this->deleteExecutionLock();
    }

    protected function getMaxExecutionTime() {
        $minutes = 10;
        return $minutes * 60;
    }
    
    protected function getExecutionLock() {
        set_error_handler(function ($errno, $errstr, $errfile, $errline ) {
            throw new \ErrorException($errstr, 0, $errno, $errfile, $errline);
        });
        
        $lockingFilePath = $this->getLockingFilePath();
        try {
            $lockFileContent = file_get_contents($lockingFilePath);
        } catch (\Exception $exc) {
            return NULL;
        }
        
        try {
            $lockFileData = unserialize($lockFileContent);
        } catch (\Exception $exc) {
            return NULL;
        }
        
        if (!is_array($lockFileData) || array ('start', 'pid') != array_keys($lockFileData)) {
            throw new \Exception(
                sprintf(
                    'Incorrect lock data format "%s": %s.',
                    $this->getLockingFilePath(),
                    var_export($lockFileData, TRUE))
            );
        }
        return $lockFileData;
    }
    
    protected function createExecutionLock() {
        file_put_contents($this->getLockingFilePath(), serialize(array(
            'start' => time(),
            'pid' => getmypid(),
        )));
    }
    
    protected function deleteExecutionLock() {
        unlink($this->getLockingFilePath());
    }
    
    private function getLockingFilePath() {
        return $this->getContainer()
            ->get('kernel')
            ->locateResource('@CherezWebDefaultBundle/Resources/locks') . DIRECTORY_SEPARATOR . $this->getName() . '.lock';
    }
    
}
