module rubis.strategies;

strategy TestTrue [ true && 1 > 0] {
	t1: (true) -> done;
}

strategy TestFalse [true && 1 > 1] {
	t1: (true) -> done;
}