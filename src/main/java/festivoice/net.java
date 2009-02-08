/*
 * festivoice
 *
 * Copyright 2009 FURUHASHI Sadayuki, KASHIHARA Shuzo, SHIBATA Yasuharu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package festivoice;

import net.festivoice.CUILauncher;
import net.festivoice.GUILauncher;

class net {
	public static void main(String[] args) throws Exception
	{
		if(args.length == 0) {
			return;
		}

		String[] nargs = new String[args.length-1];
		System.arraycopy(args, 1, nargs, 0, args.length-1);

		if(args[0].equals("-C")) {
			CUILauncher.main(nargs);
		} else if(args[0].equals("-G")) {
			GUILauncher.main(nargs);
		}

	}
}

