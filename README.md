# SwipeExitLayout

## How to use

### Step 1: Add it in your root build.gradle at the end of repositories.

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

### Step 2: Add the dependency.

```
	dependencies {
	        implementation 'com.github.JerryJin93:SwipeExitLayout:0.0.1'
	}
```

### Step3:
Just extend to the SwipeExitActivity.
```
    public class XxxActivity extends SwipeExitActivity {
        @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
            }
    }
```