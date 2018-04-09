<?php
namespace Extensions\AvalancheImagineBundle\ImagineFilter;

use Imagine\Image\ImageInterface;
use Imagine\Image\Color;
use Imagine\Image\BoxInterface;
use Imagine\Image\Point;
use Imagine\Filter\FilterInterface;
use Imagine\Image\ImagineInterface;

class PublishImage implements FilterInterface
{
    /**
     * @var BoxInterface
     */
    private $size;
  
    private $imagine;

    /**
     *
     * @param BoxInterface $size
     * @param string       $mode
     */
    public function __construct(ImagineInterface $imagine, BoxInterface $size = null)
    {
        $this->size = $size;
        $this->imagine = $imagine;
    }

    /**
     * {@inheritdoc}
     */
    public function apply(ImageInterface $image)
    {
    	if($this->size)
    	{
        	$image = $image->thumbnail($this->size, ImageInterface::THUMBNAIL_INSET);
    	}
    	
    	//TODO: добавить вотермарк
    	$image = $image->strip();
        
        return $image;
    }
}
