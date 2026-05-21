import { initializeApp, getApps } from 'firebase/app';
import { getAuth, GoogleAuthProvider, signInWithPopup } from 'firebase/auth';

const firebaseConfig = {
  apiKey: (import.meta.env.VITE_FIREBASE_API_KEY || '').trim(),
  authDomain: (import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || '').trim(),
  projectId: (import.meta.env.VITE_FIREBASE_PROJECT_ID || '').trim(),
  appId: (import.meta.env.VITE_FIREBASE_APP_ID || '').trim(),
  storageBucket: (import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || '').trim(),
  messagingSenderId: (import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || '').trim(),
  measurementId: (import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || '').trim(),
};

const placeholderPattern = /your_|replace_me|your_project|your_firebase_app_id/i;
const isValidFirebaseValue = (value) =>
  Boolean(value) && !placeholderPattern.test(value.trim());

export const isFirebaseAuthConfigured =
  isValidFirebaseValue(firebaseConfig.apiKey) &&
  isValidFirebaseValue(firebaseConfig.authDomain) &&
  isValidFirebaseValue(firebaseConfig.projectId) &&
  isValidFirebaseValue(firebaseConfig.appId);

if (!isFirebaseAuthConfigured) {
  console.warn('[Firebase] Auth not configured. Current env values:', {
    apiKey: firebaseConfig.apiKey,
    authDomain: firebaseConfig.authDomain,
    projectId: firebaseConfig.projectId,
    appId: firebaseConfig.appId,
  });
} else {
  console.info('[Firebase] Auth configured. Current env values:', {
    apiKey: firebaseConfig.apiKey,
    authDomain: firebaseConfig.authDomain,
    projectId: firebaseConfig.projectId,
    appId: firebaseConfig.appId,
  });
}

let firebaseAuth = null;
let googleProvider = null;

if (isFirebaseAuthConfigured) {
  const app = getApps().length ? getApps()[0] : initializeApp(firebaseConfig);
  firebaseAuth = getAuth(app);
  googleProvider = new GoogleAuthProvider();
  googleProvider.setCustomParameters({ prompt: 'select_account' });
}

export async function signInWithGooglePopup() {
  if (!isFirebaseAuthConfigured || !firebaseAuth || !googleProvider) {
    throw new Error('Firebase Google Auth is not configured');
  }

  const result = await signInWithPopup(firebaseAuth, googleProvider);
  const credential = GoogleAuthProvider.credentialFromResult(result);
  const oauthIdToken = credential?.idToken;
  const idToken = oauthIdToken || (await result.user.getIdToken());

  return {
    idToken,
    uid: result.user.uid,
    email: result.user.email || '',
    name: result.user.displayName || '',
  };
}
