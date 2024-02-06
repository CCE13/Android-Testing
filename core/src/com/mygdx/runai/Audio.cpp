#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// Create engine
SLObjectItf engineObject;
slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
(*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
SLEngineItf engineEngine;
(*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

// Create 3D mixer
SLMixerOptions mixerOptions;
mixerOptions.locatorType = SL_MEDIASOURCE_NULL;
mixerOptions.locator = NULL;
mixerOptions.nodeId = 0;
mixerOptions.interfaceId = SL_IID_3DCOMMIT;
mixerOptions.interface = NULL;
SLDataSource audioSource;
audioSource.pLocator = &mixerOptions;
SLDataSink audioSink;
audioSink.pLocator = &outputMixLocator;
audioSink.pFormat = &outputMixFormat;
SLObjectItf outputMixObject;
(*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, &audioSource, &audioSink);
(*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);

// Create player
SLObjectItf playerObject;
(*engineEngine)->CreateAudioPlayer(engineEngine, &playerObject, &audioSource, &audioSink, 2, ids, req);
(*playerObject)->Realize(playerObject, SL_BOOLEAN_FALSE);
SLPlayItf playerPlay;
(*playerObject)->GetInterface(playerObject, SL_IID_PLAY, &playerPlay);

// Set up source for 3D audio
SLmillibel minVolume;
(*playerPlay)->GetVolumeLevel(playerPlay, &minVolume);
SLVolumeItf playerVolume;
(*playerObject)->GetInterface(playerObject, SL_IID_VOLUME, &playerVolume);
(*playerVolume)->SetVolumeLevel(playerVolume, minVolume);
SL3DLocationItf playerLocation;
(*playerObject)->GetInterface(playerObject, SL_IID_3DLOCATION, &playerLocation);
SLVec3D location;
location.x = 0;
location.y = 0;
location.z = -1000;
(*playerLocation)->SetLocationCartesian(playerLocation, &location);

// Set up listener for 3D audio
SLObjectItf listenerObject;
(*engineEngine)->CreateListener(engineEngine, &listenerObject, 0, NULL);
(*listenerObject)->Realize(listenerObject, SL_BOOLEAN_FALSE);
SL3DLocationItf listenerLocation;
(*listenerObject)->GetInterface(listenerObject, SL_IID_3DLOCATION, &listenerLocation);
location.x = 0;
location.y = 0;
location.z = 0;
(*listenerLocation)->SetLocationCartesian(listenerLocation, &location);

// Enable 3D audio
(*playerPlay)->SetPlayState(playerPlay, SL_PLAYSTATE_PLAYING);
