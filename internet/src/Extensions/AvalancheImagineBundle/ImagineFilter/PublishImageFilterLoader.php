<?php
namespace Extensions\AvalancheImagineBundle\ImagineFilter;

use Avalanche\Bundle\ImagineBundle\Imagine\Filter\Loader\LoaderInterface;
use Imagine\Image\Box;
use Imagine\Image\Color;
use Imagine\Image\ManipulatorInterface;
use Imagine\Image\ImagineInterface;

class PublishImageFilterLoader implements LoaderInterface
{
	private $imagine;

	public function __construct(ImagineInterface $imagine)
	{
		$this->imagine = $imagine; 
	}
	
    public function load(array $options = array())
    {
    	$box = null;
		if(isset($options['size']))
		{    	
			list($width, $height) = $options['size'];
			$box = new Box($width, $height);
		}

		return new PublishImage($this->imagine, $box);
    }
}
