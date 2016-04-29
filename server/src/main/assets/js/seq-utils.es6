/**
  * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
  * See LICENSE.txt for details.
  */

define(
["immutable"],
function(
    Immutable
)
{
    /**
     * intersperse(Seq(1, 2, 3), 77) => Seq(1, 77, 2, 77, 3)
     * useful for joining react components
     */
    const intersperse = function (seq, div) {
        if (seq.size === 0) {
            return seq;
        }
        return seq
            .rest()
            .reduce(
                (xs, x, i) => xs.concat(div, x),
                Immutable.Seq.of(seq.first())
            );
    };

    return {
        intersperse: intersperse
    };
});
