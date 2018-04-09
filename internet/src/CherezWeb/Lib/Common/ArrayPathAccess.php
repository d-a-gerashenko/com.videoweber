<?php

namespace CherezWeb\Lib\Common;

class ArrayPathAccess
{
    
    private $array;

    /**
     * @param array $array
     */
    public function __construct($array)
    {
        if (!is_array($array)) {
            throw new ArrayPathAccessException;
        }
        $this->array = $array;
    }
    
    public function getArray()
    {
        return $this->array;
    }
    
    /**
     * @param mixed $path string (key_1.key_1_1...) or array (['key_1', 'key_1_1'])
     * @return mixed
     * @throws \Exception
     */
    public function getNode($path)
    {
        return self::nodeByPath($this->array, $path);
    }
    
    /**
     * @param mixed $path string (key_1.key_1_1...) or array (['key_1', 'key_1_1'])
     * @return array
     * @throws \Exception
     */
    public function getNodeArray($path)
    {
        return self::arrayByPath($this->array, $path);
    }
    
    /**
     * @param mixed $path string (key_1.key_1_1...) or array (['key_1', 'key_1_1'])
     * @return mixed Not an array.
     * @throws \Exception
     */
    public function getNodeValue($path)
    {
        return self::valueByPath($this->array, $path);
    }
    
    /**
     * @param array $node
     * @param mixed $path string (key_1.key_1_1...) or array (['key_1', 'key_1_1'])
     * @return mixed
     * @throws \Exception
     */
    public static function nodeByPath(array $node, $path)
    {
        if (!is_array($path)) {
            $path = explode('.', $path);
        }
        $breadcrumbs = [];
        foreach ($path as $key) {
            $breadcrumbs[] = $key;
            if (!(is_array($node) && array_key_exists($key, $node))) {
                throw new ArrayPathAccessException(sprintf('Array path "%s" doesn\'t exists.', implode(' -> ', $breadcrumbs)));
            }
            $node = $node[$key];
        }
        return $node;
    }
    
    /**
     * @param array $node
     * @param mixed $path string (key_1.key_1_1...) or array (['key_1', 'key_1_1'])
     * @return array
     * @throws \Exception
     */
    public static function arrayByPath(array $node, $path)
    {
        $array = self::nodeByPath($node, $path);
        if (!is_array($array)) {
            throw new ArrayPathAccessException('Node is not array. Array expected.');
        }
        return $array;
    }
    
    /**
     * @param array $node
     * @param mixed $path string (key_1.key_1_1...) or array (['key_1', 'key_1_1'])
     * @return mixed Not an array.
     * @throws \Exception
     */
    public static function valueByPath(array $node, $path)
    {
        $value = self::nodeByPath($node, $path);
        if (is_array($value)) {
            throw new ArrayPathAccessException('Node is array. Value expected.');
        }
        return $value;
    }
}