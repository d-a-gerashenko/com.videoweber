<?php

namespace CherezWeb\ProjectBundle\Form\DataTransformer;

use Symfony\Component\Form\DataTransformerInterface;

class GroupPathTransformer implements DataTransformerInterface
{

    public function transform($value)
    {
        return $value;
    }

    public function reverseTransform($value)
    {
        $value = self::mb_trim($value);
        $dirs = explode('/', $value);
        if (count($dirs)) {
            $trimed = implode('/', $dirs);
        } else {
            $trimed = $value;
        }
        return $trimed;
    }

    /**
     * Trim characters from either (or both) ends of a string in a way that is
     * multibyte-friendly.
     *
     * Mostly, this behaves exactly like trim() would: for example supplying 'abc' as
     * the charlist will trim all 'a', 'b' and 'c' chars from the string, with, of
     * course, the added bonus that you can put unicode characters in the charlist.
     *
     * We are using a PCRE character-class to do the trimming in a unicode-aware
     * way, so we must escape ^, \, - and ] which have special meanings here.
     * As you would expect, a single \ in the charlist is interpretted as
     * "trim backslashes" (and duly escaped into a double-\ ). Under most circumstances
     * you can ignore this detail.
     *
     * As a bonus, however, we also allow PCRE special character-classes (such as '\s')
     * because they can be extremely useful when dealing with UCS. '\pZ', for example,
     * matches every 'separator' character defined in Unicode, including non-breaking
     * and zero-width spaces.
     *
     * It doesn't make sense to have two or more of the same character in a character
     * class, therefore we interpret a double \ in the character list to mean a
     * single \ in the regex, allowing you to safely mix normal characters with PCRE
     * special classes.
     *
     * *Be careful* when using this bonus feature, as PHP also interprets backslashes
     * as escape characters before they are even seen by the regex. Therefore, to
     * specify '\\s' in the regex (which will be converted to the special character
     * class '\s' for trimming), you will usually have to put *4* backslashes in the
     * PHP code - as you can see from the default value of $charlist.
     *
     * @param string
     * @param charlist list of characters to remove from the ends of this string.
     * @param boolean trim the left?
     * @param boolean trim the right?
     * @return String
     */
    private static function mb_trim($string, $charlist = '\\\\s', $ltrim = true, $rtrim = true)
    {
        $both_ends = $ltrim && $rtrim;

        $char_class_inner = preg_replace(
            array('/[\^\-\]\\\]/S', '/\\\{4}/S'), array('\\\\\\0', '\\'), $charlist
        );

        $work_horse = '[' . $char_class_inner . ']+';
        $ltrim && $left_pattern = '^' . $work_horse;
        $rtrim && $right_pattern = $work_horse . '$';

        if ($both_ends) {
            $pattern_middle = $left_pattern . '|' . $right_pattern;
        } elseif ($ltrim) {
            $pattern_middle = $left_pattern;
        } else {
            $pattern_middle = $right_pattern;
        }

        return preg_replace("/$pattern_middle/usSD", '', $string);
    }
}
